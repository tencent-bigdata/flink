#!/usr/bin/env bash
################################################################################
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
# limitations under the License.
################################################################################

STAGE=$1

HERE="`dirname \"$0\"`"				# relative
HERE="`( cd \"$HERE\" && pwd )`" 	# absolutized and normalized
if [ -z "$HERE" ] ; then
	exit 1  # fail
fi

# =============================================================================
# ARTIFACTS
# =============================================================================

ARTIFACTS_DIR="${HERE}/artifacts"
rm -rf $ARTIFACTS_DIR
mkdir -p $ARTIFACTS_DIR || { echo "FAILURE: cannot create log directory '${ARTIFACTS_DIR}'." ; exit 1; }

MVN_PID="${ARTIFACTS_DIR}/watchdog.mvn.pid"
MVN_EXIT="${ARTIFACTS_DIR}/watchdog.mvn.exit"
MVN_OUT="${ARTIFACTS_DIR}/mvn.out"
TRACE_OUT="${ARTIFACTS_DIR}/jps-traces.out"

# =============================================================================
# WATCH DOG
# =============================================================================

# Number of seconds w/o output before printing a stack trace and killing $MVN
MAX_NO_OUTPUT=300
SLEEP=20

mod_time () {
	if [[ `uname` == 'Darwin' ]]; then
		eval $(stat -s $MVN_OUT)
		echo $st_mtime
	else
		echo `stat -c "%Y" $MVN_OUT`
	fi
}

the_time() {
	echo `date +%s`
}

print_stacktraces () {
	echo "=============================================================================="
	echo "The following Java processes are running (JPS)"
	echo "=============================================================================="

	jps

	local pids=( $(jps | awk '{print $1}') )

	for pid in "${pids[@]}"; do
		echo "=============================================================================="
		echo "Printing stack trace of Java process ${pid}"
		echo "=============================================================================="

		jstack -l $pid
	done
}

watchdog () {
	touch $MVN_OUT

	while true; do
		sleep $SLEEP

		time_diff=$((`the_time` - `mod_time`))

		if [ $time_diff -ge $MAX_NO_OUTPUT ]; then
			echo "=============================================================================="
			echo "Maven produced no output for ${MAX_NO_OUTPUT} seconds."
			echo "=============================================================================="

			print_stacktraces | tee $TRACE_OUT

			kill $(<$MVN_PID)

			exit 1
		fi
	done
}

# =============================================================================
# MVN
# =============================================================================

STAGE_COMPILE="compile"
STAGE_CORE="core"
STAGE_LIBRARIES="libraries"
STAGE_CONNECTORS="connectors"
STAGE_TESTS="tests"
STAGE_MISC="misc"

MODULES_CORE="\
flink-clients,\
flink-core,\
flink-java,\
flink-optimizer,\
flink-runtime,\
flink-runtime-web,\
flink-scala,\
flink-scala-shell,\
flink-streaming-java,\
flink-streaming-scala,\
flink-test-utils-parent,\
flink-test-utils-parent/flink-test-utils,\
flink-test-utils-parent/flink-test-utils-junit,\
flink-state-backends,\
flink-state-backends/flink-statebackend-rocksdb"

MODULES_LIBRARIES="\
flink-contrib/flink-storm,\
flink-contrib/flink-storm-examples,\
flink-libraries,\
flink-libraries/flink-cep,\
flink-libraries/flink-cep-scala,\
flink-libraries/flink-gelly,\
flink-libraries/flink-gelly-scala,\
flink-libraries/flink-gelly-examples,\
flink-libraries/flink-ml,\
flink-libraries/flink-python,\
flink-libraries/flink-streaming-python,\
flink-libraries/flink-table,\
flink-libraries/flink-table-common,\
flink-queryable-state/flink-queryable-state-runtime,\
flink-queryable-state/flink-queryable-state-client-java"

MODULES_CONNECTORS="\
flink-formats,\
flink-formats/flink-avro,\
flink-formats/flink-parquet,\
flink-connectors,\
flink-connectors/flink-hbase,\
flink-connectors/flink-hcatalog,\
flink-connectors/flink-hadoop-compatibility,\
flink-connectors/flink-jdbc,\
flink-connectors/flink-connector-cassandra,\
flink-connectors/flink-connector-elasticsearch,\
flink-connectors/flink-connector-elasticsearch2,\
flink-connectors/flink-connector-elasticsearch5,\
flink-connectors/flink-connector-elasticsearch6,\
flink-connectors/flink-connector-elasticsearch-base,\
flink-connectors/flink-connector-filesystem,\
flink-connectors/flink-connector-kafka-base,\
flink-connectors/flink-connector-kafka,\
flink-connectors/flink-connector-nifi,\
flink-connectors/flink-connector-rabbitmq,\
flink-connectors/flink-connector-twitter,\
flink-contrib/flink-connector-wikiedits"

MODULES_TESTS="\
flink-tests"

function get_test_modules_for_stage() {
    case $STAGE in
        (${STAGE_CORE})
            echo "-pl $MODULES_CORE"
        ;;
        (${STAGE_LIBRARIES})
            echo "-pl $MODULES_LIBRARIES"
        ;;
        (${STAGE_CONNECTORS})
            echo "-pl $MODULES_CONNECTORS"
        ;;
        (${STAGE_TESTS})
            echo "-pl $MODULES_TESTS"
        ;;
        (${STAGE_MISC})
            NEGATED_CORE=\!${MODULES_CORE//,/,\!}
            NEGATED_LIBRARIES=\!${MODULES_LIBRARIES//,/,\!}
            NEGATED_CONNECTORS=\!${MODULES_CONNECTORS//,/,\!}
            NEGATED_TESTS=\!${MODULES_TESTS//,/,\!}
            echo "-pl $NEGATED_CORE,$NEGATED_LIBRARIES,$NEGATED_CONNECTORS,$NEGATED_TESTS"
        ;;
    esac
}

LOG4J_PROPERTIES=${HERE}/log4j-travis.properties
MVN_LOGGING_OPTIONS="-Dlog.dir=${ARTIFACTS_DIR} -Dlog4j.configuration=file://$LOG4J_PROPERTIES -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
MVN_COMMON_OPTIONS="-nsu -Dflink.forkCount=4 -Dflink.forkCountTestPackage=4 -Dfast -B $MVN_LOGGING_OPTIONS"

MVN_COMPILE="mvn install -DskipTests $MVN_COMMON_OPTIONS"

MVN_TEST_MODULES=$(get_test_modules_for_stage)
MVN_TEST="mvn verify $MVN_COMMON_OPTIONS $MVN_TEST_MODULES"

# =============================================================================
# CMDS
# =============================================================================

# Start watching $MVN_OUT
watchdog &
WD_PID=$!
echo "STARTED watchdog (${WD_PID})."

echo "RUNNING '${MVN_COMPILE}'."
cd $HERE/../
( $MVN_COMPILE & PID=$! ; echo $PID >&3 ; wait $PID ; echo $? >&4 ) 3>$MVN_PID 4>$MVN_EXIT | tee $MVN_OUT
EXIT_CODE=$(<$MVN_EXIT)
echo "MVN exited with EXIT CODE: ${EXIT_CODE}."

echo "Trying to KILL watchdog (${WD_PID})."
( kill $WD_PID 2>&1 ) > /dev/null

rm $MVN_PID
rm $MVN_EXIT

# Run tests if compilation was successful
if [ $EXIT_CODE == 0 ]; then

	# Start watching $MVN_OUT
	watchdog &
    WD_PID=$!
	echo "STARTED watchdog (${WD_PID})."

	echo "RUNNING '${MVN_TEST}'."
	( $MVN_TEST & PID=$! ; echo $PID >&3 ; wait $PID ; echo $? >&4 ) 3>$MVN_PID 4>$MVN_EXIT | tee $MVN_OUT

	EXIT_CODE=$(<$MVN_EXIT)
	echo "MVN exited with EXIT CODE: ${EXIT_CODE}."

	# Make sure to kill the watchdog in any case after $MVN_TEST has completed
	echo "Trying to KILL watchdog (${WD_PID})."
	( kill $WD_PID 2>&1 ) > /dev/null

	rm $MVN_PID
	rm $MVN_EXIT
else
	echo "=============================================================================="
	echo "Compilation failure detected, skipping test execution."
	echo "=============================================================================="
fi

# only misc builds flink-dist and flink-yarn-tests
case $STAGE in
	(misc)
    for file in `find ./flink-yarn-tests/target/flink-yarn-tests* -type f -name '*.log'`; do
      TARGET_FILE=`echo "$file" | grep -Eo "container_[0-9_]+/(.*).log"`
      TARGET_DIR=`dirname	 "$TARGET_FILE"`
      mkdir -p "$ARTIFACTS_DIR/yarn-tests/$TARGET_DIR"
      cp $file "$ARTIFACTS_DIR/yarn-tests/$TARGET_FILE"
    done
	;;
esac

cd $HERE
tar -cvzf "artifacts-$STAGE.tar.gz" "artifacts"

# Exit code for Travis build success/failure
exit $EXIT_CODE


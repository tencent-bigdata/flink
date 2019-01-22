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

if [ $# -gt 0 ]; then
  TARGET_BRANCH=$1
else
  TARGET_BRANCH="master"
fi

SOURCE_BRANCH=$(git branch | grep '^*' | sed 's/* //' )

if [ "$TARGET_BRANCH" = "$SOURCE_BRANCH" ]; then
  echo "ERROR: Cannot merge branch with itself ($TARGET_BRANCH)"
  exit 1
fi

echo "Merging $TARGET_BRANCH with $SOURCE_BRANCH..."

git checkout $TARGET_BRANCH
if ! [ $? == 0 ]; then
  echo "ERROR: Cannot checkout to the target branch ($TARGET_BRANCH)"
  exit 1
fi

git fetch origin && git reset --hard origin/$TARGET_BRANCH && git clean -f -d
if ! [ $? == 0 ]; then
  echo "ERROR: Cannot properly synchronize the target branch ($TARGET_BRANCH)"
  exit 1
fi

git merge $SOURCE_BRANCH --squash
if ! [ $? == 0 ]; then
  echo "ERROR: Cannot properly merge branch $TARGET_BRANCH with branch $SOURCE_BRANCH"
  exit 1
fi

echo "Please input the story/bug associated with the commit on TAPD (hotfix): "
read ID
if [ -z "$ID" ]; then
  echo "No TAPD story/bug is associated. The commit will be pushed as a hotfix."
fi

echo "Please input the commit title: "
read TITLE
if [ -z "$TITLE" ]; then
  echo "ERROR: Cannot merge the commit with empty title."
  exit 1
fi

if [ -z "$ID" ]; then
  MSG="[hotfix] $TITLE"
else
  MSG="[TDFLINK-$ID] $TITLE"
fi

git commit -a -m "$MSG" && git push origin $TARGET_BRANCH
if [ $? == 0 ]; then
  echo "Successfully merge branch $TARGET_BRANCH with $SOURCE_BRANCH"
  git branch -D $SOURCE_BRANCH
else
  echo "ERROR: Cannot properly push the commit"
  exit 1
fi

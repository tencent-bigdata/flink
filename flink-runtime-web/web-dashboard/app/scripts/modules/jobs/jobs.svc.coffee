#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

angular.module('flinkApp')

.service 'JobsService', ($http, flinkConfig, $log, amMoment, $q, $timeout) ->

  @loadJobs = ->
    deferred = $q.defer()

    $http.get(flinkConfig.jobServer + "jobs")
    .success (data, status, headers, config) ->
      deferred.resolve(data)

    deferred.promise

  @loadJob = (jobid) ->
    deferred = $q.defer()

    $http.get flinkConfig.jobServer + "jobs/" + jobid
    .success (data, status, headers, config) =>
      deferred.resolve(data)

    deferred.promise

  @loadVertex = (jobid, vertexid) ->
    deferred = $q.defer()

    $http.get flinkConfig.jobServer + "jobs/" + jobid + "/vertices/" + vertexid
    .success (data, status, headers, config) =>
      deferred.resolve(data)

    deferred.promise

  @loadTask = (jobid, vertexid, taskindex) ->
    deferred = $q.defer()

    $http.get flinkConfig.jobServer + "jobs/" + jobid + "/vertices/" + vertexid + "/tasks/" + taskindex
    .success (data, status, headers, config) =>
      deferred.resolve(data)

    deferred.promise

  @getCheckpointConfig = (jobId) ->
    deferred = $q.defer()

    $http.get flinkConfig.jobServer + "jobs/" + jobId + "/checkpoints/config"
    .success (data) ->
      deferred.resolve(data)

    deferred.promise

  @loadCheckpoints = (jobId) ->
    deferred = $q.defer()

    $http.get flinkConfig.jobServer + "jobs/" + jobId + "/checkpoints"
    .success (data) ->
      deferred.resolve(data)

    deferred.promise

  @loadCheckpoint = (jobId, checkpointId) ->
    deferred = $q.defer()

    $http.get flinkConfig.jobServer + "jobs/" + jobId + "/checkpoints/" + checkpointId
    .success (data) ->
      deferred.resolve(data)

    deferred.promise

  @loadCheckpointVertex = (jobId, checkpointId, vertexId) ->
    deferred = $q.defer()

    $http.get flinkConfig.jobServer + "jobs/" + jobId + "/checkpoints/" + checkpointId + "/vertices/" + vertexId
      .success (data) ->
        deferred.resolve(data)

    deferred.promise

  @stateList = ->
    [ 
      # 'CREATED'
      'SCHEDULED'
      'DEPLOYING'
      'RUNNING'
      'FINISHED'
      'FAILED'
      'CANCELING'
      'CANCELED'
    ]

  @translateLabelState = (state) ->
    switch state.toLowerCase()
      when 'finished' then 'success'
      when 'failed' then 'danger'
      when 'scheduled' then 'default'
      when 'deploying' then 'info'
      when 'running' then 'primary'
      when 'canceling' then 'warning'
      when 'pending' then 'info'
      when 'total' then 'black'
      else 'default'

  @setEndTimes = (list) ->
    angular.forEach list, (item, jobKey) ->
      unless item['end-time'] > -1
        item['end-time'] = item['start-time'] + item['duration']

  @processVertices = (data) ->
    angular.forEach data.vertices, (vertex, i) ->
      vertex.type = 'regular'

    data.vertices.unshift({
      name: 'Scheduled'
      'start-time': data.timestamps['CREATED']
      'end-time': data.timestamps['CREATED'] + 1
      type: 'scheduled'
    })

  @getNode = (nodeid) ->
    seekNode = (nodeid, data) ->
      for node in data
        return node if node.id is nodeid
        sub = seekNode(nodeid, node.step_function) if node.step_function
        return sub if sub

      null

    deferred = $q.defer()

    deferreds.job.promise.then (data) =>
      foundNode = seekNode(nodeid, currentJob.plan.nodes)

      foundNode.vertex = @seekVertex(nodeid)

      deferred.resolve(foundNode)

    deferred.promise

  @seekVertex = (nodeid) ->
    for vertex in currentJob.vertices
      return vertex if vertex.id is nodeid

    return null

  @getVertex = (vertexid) ->
    deferred = $q.defer()

    deferreds.job.promise.then (data) =>
      vertex = @seekVertex(vertexid)

      $http.get flinkConfig.jobServer + "jobs/" + currentJob.jid + "/vertices/" + vertexid + "/subtasktimes"
      .success (data) =>
        # TODO: change to subtasktimes
        vertex.subtasks = data.subtasks

        deferred.resolve(vertex)

    deferred.promise

  @getSubtasks = (vertexid) ->
    deferred = $q.defer()

    deferreds.job.promise.then (data) =>
      # vertex = @seekVertex(vertexid)

      $http.get flinkConfig.jobServer + "jobs/" + currentJob.jid + "/vertices/" + vertexid
      .success (data) ->
        subtasks = data.subtasks

        deferred.resolve(subtasks)

    deferred.promise

  @getTaskManagers = (vertexid) ->
    deferred = $q.defer()

    deferreds.job.promise.then (data) =>
      # vertex = @seekVertex(vertexid)

      $http.get flinkConfig.jobServer + "jobs/" + currentJob.jid + "/vertices/" + vertexid + "/taskmanagers"
      .success (data) ->
        taskmanagers = data.taskmanagers

        deferred.resolve(taskmanagers)

    deferred.promise

  @getAccumulators = (vertexid) ->
    deferred = $q.defer()

    deferreds.job.promise.then (data) =>
      # vertex = @seekVertex(vertexid)
      console.log(currentJob.jid)
      $http.get flinkConfig.jobServer + "jobs/" + currentJob.jid + "/vertices/" + vertexid + "/accumulators"
      .success (data) ->
        accumulators = data['user-accumulators']

        $http.get flinkConfig.jobServer + "jobs/" + currentJob.jid + "/vertices/" + vertexid + "/subtasks/accumulators"
        .success (data) ->
          subtaskAccumulators = data.subtasks

          deferred.resolve({ main: accumulators, subtasks: subtaskAccumulators })

    deferred.promise

  @loadExceptions = (jobId) ->
    deferred = $q.defer()

    $http.get flinkConfig.jobServer + "jobs/" + jobId + "/exceptions"
    .success (exceptions) ->
      deferred.resolve(exceptions)

    deferred.promise

  @

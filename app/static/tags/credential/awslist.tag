<form class="form-horizontal" role="document">
    <!-- role: 'document' - non-editable "form" -->

    <div class="form-group">
        <label class="col-sm-3 control-label" for="name">{{msg.name_label}}</label>

        <div class="col-sm-9">
            <p id="name" class="form-control-static">{{credential.name}}</p>
        </div>
        <!-- .col-sm-9 -->
    </div>
    <!-- .form-group -->
    <div class="form-group" ng-show="credential.description">
        <label class="col-sm-3 control-label" for="awsdescriptionfield">{{msg.description_label}}</label>

        <div class="col-sm-9">
            <p id="awsdescriptionfield" class="form-control-static">{{credential.description}}</p>
        </div>
        <!-- .col-sm-9 -->
    </div>
    <!-- .form-group -->
    <div class="form-group" ng-if="credential.parameters.selector=='role-based'">
        <label class="col-sm-3 control-label" for="roleArn">{{msg.credential_aws_form_iam_label}}</label>

        <div class="col-sm-9">
            <p id="roleArn" class="form-control-static">{{credential.parameters.roleArn}}</p>
        </div>
        <!-- .col-sm-9 -->
    </div>
    <div class="form-group" ng-if="credential.parameters.selector=='key-based'">
        <label class="col-sm-3 control-label" for="roleArn">{{msg.credential_aws_form_access_key_label}}</label>

        <div class="col-sm-9">
            <p id="roleArn" class="form-control-static">{{credential.parameters.accessKey}}</p>
        </div>
        <!-- .col-sm-9 -->
    </div>
    <div class="form-group" ng-show="credential.topologyId">
        <label class="col-sm-3 control-label" for="credential-topology">{{msg.credential_form_topology_label}}</label>

        <div class="col-sm-9">
            <p id="credential-topology" class="form-control-static">{{getTopologyNameById(credential.topologyId)}}</p>
        </div>
        <!-- .col-sm-9 -->
    </div>
</form>
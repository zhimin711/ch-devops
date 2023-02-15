# ch-devops
### 上传配置 
```
scp -r src/main/docker/Dockerfile zhimin@192.168.199.194:/home/zhimin/docker/ch-devops
scp -r web/target/ch-devops-1.0.0-SNAPSHOT.jar zhimin@192.168.199.194:/home/zhimin/docker/ch-devops
```
### 打包
```
docker build -t ch-devops:v2 .
```

### 启动
```
docker run --name ch-devops \
-p 7000:7000 \
-d ch-devops:v1 ;
```
```
docker run --name ch-devops \
--net=none \
 -v /home/zhimin/share/logs:/mnt/share/logs  \
 -m 512M --memory-swap -1 \
-d ch-devops:v1 ;
```
### 重启 停止 删除
```
docker restart ch-devops;

docker stop ch-devops;
docker rm ch-devops;
docker rmi ch-devops:v1;

```

### 分配网络
```
pipework br0 ch-devops 192.168.1.20/24@192.168.1.1;
```
jenkins pipeline job
```groovy
// parameters {
//     gitParameter branchFilter: 'origin/(.*)', defaultValue: 'master', name: 'BRANCH', type: 'PT_BRANCH'
// }20220708142519
def label = "jnlp-${JOB_NAME}"
def app_name = "ch-devops"
def img_name = "ch-devops:${DATETIME}"
def img_namespace = "ch"
def docker_api = "-H tcp://192.168.0.253:2375"
// def hub_addr = "192.168.0.253:8083"
def hub_addr = "registry.kubeoperator.io:8083"
def k8s_url = "https://192.168.0.252:8443"

podTemplate(label: label, cloud: 'kubernetes', inheritFrom: 'jenkins-slave-maven') {
    node(label) {
            stage('Checkout Project') {
                echo "1.Clone Project "
                git credentialsId: 'CHGitee2', url: 'https://gitee.com/ch-cloud/ch-devops.git/', branch: "${params.BRANCH}"
            }
            stage('Build project') {
                container('maven') {
                    // sh 'git config --global url."https://".insteadOf ssh://git@'
                    echo "2.Build Project Deploy Package File"
                    sh 'mvn clean package -U'
                    sh "cp ${WORKSPACE}/../apache-skywalking-java-agent-8.11.0.tgz ${WORKSPACE}/web/target"
                }
            }
            stage('Build Image') {
                echo "3.Build Project Docker Image"
                // sh "cd ${WORKSPACE}/web"
                container('docker') {
                    sh "docker ${docker_api} build -t ${img_name} -f ${WORKSPACE}/web/src/main/docker/Dockerfile ${WORKSPACE}/web/target"
                    sh "docker ${docker_api} tag ${img_name} ${hub_addr}/${img_namespace}/${img_name}"
                }

            }
            stage('Push Image') {
                echo "4.Push Project Docker Image"
                container('docker')  {
                    withCredentials([usernamePassword(credentialsId: 'Nexus', passwordVariable: 'dockerPassword', usernameVariable: 'dockerUser')]) {
                        sh "docker ${docker_api} login -u ${dockerUser} -p ${dockerPassword} ${hub_addr}"
                        sh "docker ${docker_api} push ${hub_addr}/${img_namespace}/${img_name}"
                        sh "docker ${docker_api} rmi ${hub_addr}/${img_namespace}/${img_name} ${img_name}"
                    }
                }
            }
            stage('Deploy Image') {
                echo "5.Deploy Project Docker Image"
                // kubernetesDeploy configs: 'xxx/k8s-deploy.yaml', kubeConfig: [path: ''], kubeconfigId: 'kubeoperator', secretName: '', ssh: [sshCredentialsId: '*', sshServer: ''], textCredentials: [certificateAuthorityData: '', clientCertificateData: '', clientKeyData: '', serverUrl: 'https://']
                container ('docker') {
                    script{
                        out=sh(script:"ls ./kubectl",returnStatus:true)
                        println "--------------"
                        println out
                        if(out == 0){
                            println "file is exist"
                        } else if(out == 1 || out == 2){
                            println "file is not exist"
//                        sh 'curl -LO "https://storage.googleapis.com/kubernetes-release/release/v1.20.5/bin/linux/amd64/kubectl"'
                            sh 'cp ../kubectl .'
                            sh 'chmod u+x ./kubectl'
                        } else {
                            error("command is error,please check")
                        }
                    }
                    withKubeConfig([credentialsId:'kubeMaster'
                                    ,serverUrl: "${k8s_url}"
                                    ,namespace: "ch"]) {
                        sh "./kubectl set image deployment/${app_name} *=${hub_addr}/${img_namespace}/${img_name}"
                    }
                }
            }
    }
}
```
```groovy
def jobName = "ch-devops-ci"
def maxNumber = 25    // 保留的最小编号，意味着小于该编号的构建都将被删除

Jenkins.instance.getItemByFullName(jobName).builds.findAll {
  it.number <= maxNumber
}.each {
  it.delete()
}
```
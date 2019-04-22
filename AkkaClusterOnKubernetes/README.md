# Akka Cluster on Kubernetes:
Akka cluster that creates a configurable amount of master and workers to count words.
This akka cluster can be run on a kubernetes cluster.

#### How to run it on kubernetes (Minikube locally)

***Step 1 - Installing***
- If you don't have a Dockerhub account, create one. If you don't have Docker, Minikube and kubectl installed, go install it! 

***Step 2 - Package and Publish***
-  Package the application into docker:
```
sbt assembly
```
- Build the docker image:
```
docker build -t {dockerhub namespace}/akka-cluster .
```
- Publish the image into your dockerhub account
```
docker push {dockerhub namespace}/akka-cluster
```
***Step 3 - Deploying on Minikube***
- Deploy etcd
```
kubectl apply -f kubernetes/etcd.yaml
```
- Deploy your app
```
kubectl apply -f kubernetes/wordcount.yaml
```
#### How to run it on kubernetes (Cloud account)
***Todo***

## Author
* **Antonio Maldonado**

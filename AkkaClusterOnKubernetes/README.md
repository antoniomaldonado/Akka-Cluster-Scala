# Akka Cluster on Kubernetes:
Akka cluster that creates a configurable amount of master and workers to count the words in a book located on a url that we will pass as a parameter.
This akka cluster can be run on a kubernetes cluster.

## How to run it on kubernetes (Minikube locally)

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
- Start minikube
```
minikube start
```
- Deploy etcd
```
kubectl apply -f kubernetes/etcd.yaml
```
- Deploy your app
```
kubectl apply -f kubernetes/wordcount.yaml
```
***Test the app***

We don't have a external IP to curl minikube by default but thre is a easy workaround, we can create a debug container with the following command to curl our app from this debug pod
```
kubectl run bounce --image=freakymaster/debug -it bash
```
We also need to know the IP of the wordcount service
```
kubectl get services
```
Now we can test the app with a curl command from our debug pod. We need to pass as a message the url containing the book containing the words that we want to count. In this case I ll pass the book Pride and prejudice.
```
curl "http://{IP of the wordcount pod}:8080/?msg=https://www.gutenberg.org/files/1342/1342-0.txt"
```
log the wordcount pod
```
kubectl logs {your running wordcount pod}
```
## How to run it on kubernetes (Cloud account)

Follow the same steps 1 and 2

***Step 3 - Create the cluster on gcloud***
- Stop minikube, you will deal now with gcloud. 
```
gcloud container clusters create {lowercase only name of your cluster} --num-nodes=4
```

***Step 4: Expose an external IP***

***(TODO)***
Creating a Service of type NodePort or LoadBalancer

***Step 5: Deploy your application***
- Deploy etcd
```
kubectl apply -f kubernetes/etcd.yaml
```
- Deploy your app
```
kubectl apply -f kubernetes/wordcount.yaml
```
***Test the app***

We need the IP of the wordcount service
```
kubectl get services
```
Now we can test the app with a curl command from our debug pod. We need to pass as a message the url containing the book containing the words that we want to count. In this case I ll pass the book Pride and prejudice.
```
curl "http://{IP of the wordcount pod}:8080/?msg=https://www.gutenberg.org/files/1342/1342-0.txt"
```
log the wordcount pod
```
kubectl logs {your running wordcount pod}
```
## Author
* **Antonio Maldonado**

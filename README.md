[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)
# Create a Java Web Application using Embedded Tomcat

This tutorial will show you how to create a simple Java web application using embedded Tomcat.

## Prerequisites

* Basic Java knowledge, including an installed version of the JVM and Maven.
* Basic Git knowledge, including an installed version of Git.
* A Java web application. If you don't have one follow the first step to create an example. Otherwise skip that step.

## Skip The Application Creation

If you want to skip the creation steps you can clone the finished sample and then skip to the 'Deploy Your Application to Heroku' section:

```
$ git clone git@github.com:heroku/devcenter-embedded-tomcat.git
```

## Follow the Guide

If you would like to create the application yourself, then follow the Dev Center guide on how to [Create a Java Web Application using Embedded Tomcat](https://devcenter.heroku.com/articles/create-a-java-web-application-using-embedded-tomcat).

## Run

```shell
mvn package
#./mvnw package
sh -x ./target/bin/webapp
curl http://localhost:8080/hello
```

# standalone VS embedded
Bootstrap -> Catalina -> ...
Tomcat ->                ...

# A tomcat impl - not compliant to servlet spec
https://github.com/feifa168/mytomcat
1.启动ServerSocket，封装在MyTomcat中
2.accept得到Socket client
3.调用disptach(new MyRequest(client.getInputStream), new MyResponse(client.getOutputStream))
4.disptch内部根据request的url查询得到对应的servlet包名
5.调用Class.forName(servletName)得到实际的servlet类
6.用反射，netInstance或构造函数创建对象 servletObj
7.servletObj调用service分发请求和应答
8.service内部根据url中的请求方法判断调用doGet或doPost

1## shardingsphere-seata-spring-boot-test

- For https://github.com/apache/shardingsphere/issues/29712 .

- This example uses ShardingSphere's Seata-AT mode integration and performs
  `CRUD` unit testing against the postgresql database on Spring Boot 3.
  This is all done on GraalVM For JDK 21.

- Of course we are **not** discussing GraalVM Native Image here.
  This Example is modified from nativeTest on the ShardingSphere side.

- Both Postgresql and Seata server are created by `testcontainers-java`.
  You do not need to start the middleware of Postgresql and Seata server in advance.

- Currently git intentionally bypasses the use of `org.springframework.boot:spring-boot-testcontainers`
  because manually managing `testcontainers-java`'s Java API is more interpretable for unit testing.

- Execute the following command to run unit tests under Ubuntu 22.04.3.
  This requires `SDKMAN!` and `Docker` engine to be installed in advance.
  You also need to ensure that the local `37403` port is available.
  Due to limitations of Seata Client,
  we cannot dynamically define the contents of `file.conf` through the Java API.

```shell
sdk install java 21.0.2-graalce
sdk use java 21.0.2-graalce

git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
git reset --hard de192a30c9460bb94385c2a88e766025b211f906
./mvnw clean install -Prelease -T1C -DskipTests -Djacoco.skip=true -Dcheckstyle.skip=true -Drat.skip=true -Dmaven.javadoc.skip=true

cd ../

git clone git@github.com:linghengqian/shardingsphere-seata-spring-boot-test.git
cd ./shardingsphere-seata-spring-boot-test/
./mvnw -e -T1C clean test
```

- This involves the following things.
  - Snapshot version of Apache ShardingSphere 5.5.0
  - Seata Server 2.0 and Seata Spring Boot Starter 2.0
  - Spring Boot 3.2.2
  - Three independent Postgresql Server 16.2 instances
  - GraalVM CE For JDK21

- Obviously, the current Example has unit tested all the functions associated with the Sharding feature.
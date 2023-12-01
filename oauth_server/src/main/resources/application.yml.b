server:
  port: 8082
spring:
  application:
    name: oauth-service
  cloud:
    nacos:
      server-addr: 192.168.43.29:8848
  datasource:
    url: jdbc:mysql://localhost:3306/object_storage_system?allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: bbbbbblackfree
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: 192.168.43.29
    port: 6379
  mail:
    host: smtp.qq.com
    username: 1370441324@qq.com
    password: jbsdymeewtogjdgh
    properties:
      mail:
        smtp:
          port: 465
          starttls:
            enable: true
            required: true
          ssl:
            enable: true
    default-encoding: utf-8
mybatis:
  mapper-locations: "classpath:mapper/*.xml"
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

oauth-user-details:
  client_id: member
  client-secret: 123123
  grant-type: password
  scope: read
file:
  windows:
    real-path: /object_storage_system/db/pictures/
    vir-path: /file/
jwt:
  keyPairName: jwt.jks
  keyPairAlias: jwt
  keyPairSecret: 123123
  keyPairStoreSecret: 123123
logging:
  level:
    web: debug
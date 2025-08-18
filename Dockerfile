FROM tomcat:10-jre21

COPY target/blog.war /usr/local/tomcat/webapps/blog.war

EXPOSE 8080 8000
ENTRYPOINT ["catalina.sh", "run", "--", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000"]
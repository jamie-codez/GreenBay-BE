FROM adoptopenjdk/openjdk14

ENV VERTICLE_HOME /app
ENV VERTICLE_FILE greenbay_api-v1.0.1-all.jar

WORKDIR $VERTICLE_HOME

EXPOSE 8000
COPY /build/libs/$VERTICLE_FILE $VERTICLE_HOME

ENTRYPOINT ["sh","-c"]
CMD ["exec java -jar $VERTICLE_FILE"]
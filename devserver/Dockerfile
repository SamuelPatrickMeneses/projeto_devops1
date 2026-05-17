FROM eclipse-temurin:17-jdk

RUN mkdir -p /opt/gradle
#RUN wget -O /opt/gradle/gradle.zip https://services.gradle.org/distributions/gradle-8.7-bin.zip
RUN apt update -y && apt install zip  iproute2 -y
#RUN unzip /opt/gradle/gradle.zip -d /opt/gradle && chmod 777 -R /opt/gradle/gradle-8.7

COPY --chmod=777 --chown=ubuntu:ubuntu . /home/ubuntu

RUN [ ! -f /home/ubuntu/server/postgresql-42.7.3.jar ] && wget -O /home/ubuntu/server/postgresql-42.7.3.jar https://jdbc.postgresql.org/download/postgresql-42.7.3.jar || true

# Cria o usuário gradle (UID 1000) para a JVM carregar bibliotecas nativas
# UID e GID 1000 já existem na imagem base (eclipse-temurin)
#RUN useradd -u 1000 -g 1000 -o -m -s /bin/bash gradle


WORKDIR /home/ubuntu
USER ubuntu

#ARG _GRADLE_USER_HOME

ENV GRADLE_USER_HOME=/home/ubuntu/.gradle-home

RUN ./gradlew clean wrapper --no-daemon --gradle-user-home /home/ubuntu/.gradle-home

CMD ["./gradlew", "clean", ":app:libertyDev", "--no-daemon", "--skipTests", "--gradle-user-home", "$GRADLE_USER_HOME"]


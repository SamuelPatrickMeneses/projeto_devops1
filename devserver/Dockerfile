FROM eclipse-temurin:17-jdk

RUN mkdir -p /opt/gradle
#RUN wget -O /opt/gradle/gradle.zip https://services.gradle.org/distributions/gradle-8.7-bin.zip
RUN apt update -y && apt install zip  iproute2 -y
#RUN unzip /opt/gradle/gradle.zip -d /opt/gradle && chmod 777 -R /opt/gradle/gradle-8.7

# Cria o usuário gradle (UID 1000) para a JVM carregar bibliotecas nativas
# UID e GID 1000 já existem na imagem base (eclipse-temurin)
#RUN useradd -u 1000 -g 1000 -o -m -s /bin/bash gradle

COPY --chmod=777 --chown=ubuntu:ubuntu . /home/ubuntu

WORKDIR /home/ubuntu
USER ubuntu

ARG _GRADLE_USER_HOME

ENV GRADLE_USER_HOME=${_GRADLE_USER_HOME}

RUN ./gradlew wrapper

CMD ["./gradlew", "clean", ":app:libertyDev", "--no-daemon", "--skipTests"]


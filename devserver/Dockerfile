FROM eclipse-temurin:17-jdk

RUN mkdir -p /opt/gradle
RUN wget -O /opt/gradle/gradle.zip https://services.gradle.org/distributions/gradle-8.7-bin.zip
RUN apt update -y && apt install zip  iproute2 -y
RUN unzip /opt/gradle/gradle.zip -d /opt/gradle && chmod 777 -R /opt/gradle/gradle-8.7

# Cria o usuário gradle (UID 1000) para a JVM carregar bibliotecas nativas
# UID e GID 1000 já existem na imagem base (eclipse-temurin)
RUN useradd -u 1000 -g 1000 -o -m -s /bin/bash gradle

COPY --chmod=777 --chown=1000:1000 . /home/ubuntu

WORKDIR /home/ubuntu
USER gradle

CMD ["/opt/gradle/gradle-8.7/bin/gradle", "clean", ":app:libertyDev", "--no-daemon", "--skipTests"]


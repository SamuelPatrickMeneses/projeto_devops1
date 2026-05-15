FROM eclipse-temurin:17-jdk

RUN mkdir -p /opt/gradle
RUN wget -O /opt/gradle/gradle.zip https://services.gradle.org/distributions/gradle-8.7-bin.zip
RUN apt update -y && apt install zip  iproute2 -y
RUN unzip /opt/gradle/gradle.zip -d /opt/gradle && chmod 777 -R /opt/gradle/gradle-8.7

# CORREÇÃO: Copia recursivamente tudo (arquivos e pastas) mantendo a estrutura
COPY --chmod=777 --chown=1000:1000 . /home/ubuntu

WORKDIR /home/ubuntu
USER 1000:1000

#ENTRYPOINT ./devserver/docker-entrypoint.sh

# CORREÇÃO: Sintaxe correta para exclusão de tarefas de submódulos no Gradle (sem os dois pontos iniciais no -x)
CMD ["/opt/gradle/gradle-8.7/bin/gradle", "clean", ":app:libertyDev", "--no-daemon", "--skipTests"]


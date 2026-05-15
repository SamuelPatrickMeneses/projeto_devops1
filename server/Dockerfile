FROM icr.io/appcafe/open-liberty:full-java17-openj9-ubi

# Add Liberty server configuration including all necessary features
COPY --chown=1001:0  ./server/server.xml /config/

# Modify feature repository (optional)
# A sample is in the 'Getting Required Features' section below
#COPY --chown=1001:0 featureUtility.properties /opt/ol/wlp/etc/

# This script will add the requested XML snippets to enable Liberty features and grow image to be fit-for-purpose using featureUtility.
# Only available in 'kernel-slim'. The 'full' tag already includes all features for convenience.

# Add interim fixes (optional)
#COPY --chown=1001:0  interim-fixes /opt/ol/fixes/

# Add app


COPY --chown=1001:0 --chmod=777 ./app/build/libs/app.war /config/apps/
COPY --chown=1001:0 --chmod=777  ./server/postgresql-42.7.3.jar /opt/ol/wlp/usr/shared/resources/

EXPOSE 8080

# This script will add the requested server configurations, apply any interim fixes and populate caches to optimize runtime
RUN configure.sh

RUN featureUtility installServerFeatures --acceptLicense defaultServer


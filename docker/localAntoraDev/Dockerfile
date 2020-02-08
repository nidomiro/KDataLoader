FROM node:12-alpine
WORKDIR /workdir
RUN apk update && apk upgrade
#install browser-sync
RUN npm install -g browser-sync && npm install -g @antora/cli@2.2 @antora/site-generator-default@2.2
RUN mkdir /public

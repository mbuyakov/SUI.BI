FROM nginx:alpine
COPY ./nginx/templates_conf.d /etc/nginx/templates
COPY ./dist /usr/share/nginx/html
COPY ./nginx/dns-to-env.sh /dns-to-env.sh
RUN echo "$(cat /dns-to-env.sh)$(cat /docker-entrypoint.sh)" > /docker-entrypoint.sh
EXPOSE 3080

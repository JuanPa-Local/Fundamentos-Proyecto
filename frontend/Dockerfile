FROM nginx:alpine
# Copiar todos los archivos HTML, CSS y JS al directorio por defecto de nginx
COPY . /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

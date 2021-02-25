# HTTPS : Générer un certificat (Let's encrypt) et configurer Nginx

## Installer Certbot

Cerbot est un client acme permettant de certifier la possession d'un serveur

Reference : [Cerbot doc](https://certbot.eff.org/lets-encrypt/debianbuster-nginx)

Cerbot est distribué au travers de snaps il faut donc commencer par installer snapd

### Installer snapd

`sudo apt-get install snapd`

puis installer le core : `sudo snap install core` puis le mettre à jour si nécessaire `sudo snap refresh core`

### Installer certbot

`sudo snap install --classic certbot`

## Revoir la configuration de nginx

Nginx doit maintenant avoir un `server_name` avec la valeur du domainName à certifier

De plus on souhaite retirer toutes les entraves à ce que Let's Encrypt joigne notre serveur sur le port 80.

On se retrouve donc avec un `/etc/nginx/sites-enabled/sharad` :

```
server {
  listen 80;
  server_name <domainName>;
}
```

Puis restart nginx : `sudo systemctl restart nginx`

### Générer le certificat et changer la configuration Nginx

#### Certbot pour le gros du travail

Certbot se charge de tout cela pour nous :
`sudo certbot --nginx -d <doaminName>`

Output :

```
Deploying Certificate to VirtualHost /etc/nginx/sites-enabled/sharad
Redirecting all traffic on port 80 to ssl in /etc/nginx/sites-enabled/sharad

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
Your existing certificate has been successfully renewed, and the new certificate
has been installed.

The new certificate covers the following domains:
https://<domainName>
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
```

#### Un peu de ménage dans la configuration Nginx

Cerbot s'est permis de modifier notre configuration Nginx. Remettons un peu d'ordre dedans pour rediriger le traffic
sur 443 vers notre application en local. On aboutit à :

```
server {
    server_name ns328297.ip-37-187-113.eu;

    location / {
      proxy_pass http://localhost:8080/;
    }

    listen 443 ssl; # managed by Certbot
    ssl_certificate /etc/letsencrypt/live/ns328297.ip-37-187-113.eu/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/ns328297.ip-37-187-113.eu/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot

}

server {
    if ($host = ns328297.ip-37-187-113.eu) {
        return 301 https://$host$request_uri;
    } # managed by Certbot


  listen 80;
  server_name ns328297.ip-37-187-113.eu;
    return 404; # managed by Certbot


}
```


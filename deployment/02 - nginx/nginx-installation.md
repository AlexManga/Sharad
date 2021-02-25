# Exposer le port 80 et rediriger vers l'application qui écoute sur 127.0.0.1:8080

`sudo apt-get install nginx`

La configuration de nginx se situe dans `/etc/nginx/nginx.conf`.

Elle importe :

1) les fichiers `*.conf` de `/etc/nginx/conf.d/`
2) les fichiers de `/etc/nginx/sites-enabled`

Le lien `/etc/nginx/sites-enabled/default` prend souvent le pas. Il faut ainsi le remplacer par un fichier
du type: 

```
# à mettre dans /etc/nginx/sites-enabled/
server {
  listen 80;

  location / {
    proxy_pass http://localhost:8080/;
  }
}
```

Ainsi l'application peut écouter sur localhost et ne pas répondre aux requêtes venant de l'extérieur. Toutes les requêtes sur /:80 lui sont transférées sur localhost:8080.

Nginx redémarre automatiquement au reboot du serveur.
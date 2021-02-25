## Prérequis opérationnels pour déployer l'application :

- Avoir un serveur exposant une URL publique
- Installer java 14
- Avoir un accès sftp et ssh
- Avoir configuré le serveur pour :
  - Accepter les requêtes sur le port 80 et les rediriger vers localhost:8080 => nginx
  - Si le serveur redémarre, nginx et l'application redémarrent d'eux-mêmes => systemd
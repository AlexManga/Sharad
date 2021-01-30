# Backlog

- [BACK] POST /note : Save notes on disk to enable persistence on server restart
  - Store on a local storage space ($cwd/.sharad/data ?)
    - on a default local storage space
    - on a configurable local storage space
      - make different configurations for prod and dev modes
    - synchronise storage actions
      - on a server-wide resource ? based on the note ID ?
  - Attribute an ID to each created note
    - generate a unique ID
    - store ID in note metadata
    - return note ID in response
  - [BACK] handle 400 on null or empty content of a note
  - [BACK] sanitize note content
    
- [BACK] DELETE /note
- [BACK] PUT /note
- [BACK] handle markdown content
- [BACK] HTTPS connection
- [BACK] secure content
  - encrypt files stored on disk
  - encrypt content between front and back ?
- [BACK/FRONT] resource access control
  - sharing resource with a "friend"
  - enabling groups

- [BACK/FRONT] user authentication
  - HTTPS 2-ways ?
- [BACK/FRONT] PWA
  - make the app installable from a browser (don't work well on firefox)
  - enable notifications
- [FRONT] display notes on startup
- [TEST] e2e
  - test user journey (ex : start from an empty storage, post two notes then get them)
- [FRONT] display and edit markdown
- [FRONT] caching strategy
    
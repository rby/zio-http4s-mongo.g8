# Giter8 template for ZIO + HTTP4S + TAPIR + MONGO


# Project structure

```
- root
  - server

```

# Run

```
$ sbt
> project server
> run
```

```
curl localhost:9000/ping
curl localhost:9000/v1/greet -H'Content-type: application/json' -d '"John Doe"'
```

# Docs

```
http://localhost:9000/docs
```
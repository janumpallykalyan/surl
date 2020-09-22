docker-compose -f mongoSetup/shard1/docker-compose.yml down
docker-compose -f mongoSetup/shard2/docker-compose.yml down
docker-compose -f mongoSetup/config-server/docker-compose.yml down
docker-compose -f mongoSetup/mongos/docker-compose.yml down
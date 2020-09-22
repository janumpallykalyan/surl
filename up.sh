echo "Remeber to update private IP in mongoSetup/mongos/docker-compose"
docker-compose -f mongoSetup/shard1/docker-compose.yml up -d
docker-compose -f mongoSetup/shard2/docker-compose.yml up -d
docker-compose -f mongoSetup/config-server/docker-compose.yml up -d
docker-compose -f mongoSetup/mongos/docker-compose.yml up -d
#!/bin/bash

sudo docker stop ctn_neurone_conector
sudo docker rm ctn_neurone_conector
sudo docker rmi img_neurone_conector


sudo docker build -t img_neurone_conector .
sudo docker run --network=host  -p 8081:8081   --name ctn_neurone_conector -d img_neurone_conector
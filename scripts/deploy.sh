#!/bin/bash
DEPLOY_PATH=$(ls /home/ec2-user/app/deploy/*SNAPSHOT.jar)
echo ">>> 현재 실행중인 애플리케이션 pid 확인"
CURRENT_PID=$(pgrep -f jar)

if [ -z $CURRENT_PID ]
then
  echo ">>> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
  echo ">>> kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi

echo ">>> DEPLOY_JAR 배포"
nohup java -jar $DEPLOY_PATH > /dev/null 2> /dev/null < /dev/null &
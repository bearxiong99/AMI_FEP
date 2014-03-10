cd /home/start/
./stopgate.sh

cd /usr/bin/
svn update --username=gaoll --password=gaoll https://172.16.241.21:8443/svn/ami3.0/trunk/fep/FEP_gate_zj /opt/fep/FEP_gate_zj --no-auth-cache


cd /opt/apache-ant-1.8.4/bin/
./ant -buildfile  /opt/Fep-Source/FEP_gate_zj/build.xml

cd /home/start/
./startgate.sh

cd /home/start/
./stopdp.sh

cd /usr/bin/
svn update --username=gaoll --password=gaoll https://172.16.241.21:8443/svn/ami3.0/trunk/fep/FEP_DlmsPolling /opt/Fep-Source/FEP_DlmsPolling --no-auth-cache


cd /opt/apache-ant-1.8.4/bin/
./ant -buildfile  /opt/Fep-Source/FEP_DlmsPolling/build.xml

cd /home/start/
./startdp.sh

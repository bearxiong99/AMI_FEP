cd /home/start/
./stopdr.sh

cd /usr/bin/
svn update --username=gaoll --password=gaoll https://172.16.241.21:8443/svn/ami3.0/trunk/fep/FEP_DlmsReread /opt/Fep-Source/FEP_DlmsReread --no-auth-cache


cd /opt/apache-ant-1.8.4/bin/
./ant -buildfile  /opt/Fep-Source/FEP_DlmsReread/build.xml

cd /home/start/
./startdr.sh

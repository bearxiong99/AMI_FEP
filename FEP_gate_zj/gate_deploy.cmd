title FEP

c:
cd C:\"Program Files"\VisualSVN Server\bin
svn update --username=gaoll --password=gaoll https://172.16.241.21:8443/svn/ami3.0/trunk/fep/FEP_gate_zj E:\FEP-Source\FEP_gate_zj  --no-auth-cache 


D:
cd D:\Tools\apache-ant-1.8.4\bin


call ant -buildfile  E:\FEP-Source\FEP_gate_zj\build.xml


pause
set -x
pip install --target ./package -r requirements.txt
cd package
zip -r ../LoadBalancerInfoFunction.zip .
cd ..
zip -g LoadBalancerInfoFunction.zip loadbalancerinfo.py

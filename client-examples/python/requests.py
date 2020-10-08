import requests

import sys

path_to_key = sys.argv[1]
path_to_cert = sys.argv[2]
url = sys.argv[3]

r = requests.get(url, cert=(path_to_cert, path_to_key))
print(r.json())

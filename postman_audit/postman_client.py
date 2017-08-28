import requests


class PostmanClient:
    BASE_URL = "https://api.getpostman.com"

    def __init__(self, key):
        self.session = requests.Session()
        self.session.headers.update({"X-Api-Key": key})

    def get_collections(self, ):
        response = self.session.get("%s/collections" % self.BASE_URL)
        response.raise_for_status()
        return response.json()

    def get_collection(self, uid):
        response = self.session.get("%s/collections/%s" % (self.BASE_URL, uid))
        response.raise_for_status()
        return response.json()

    def update_collection(self, uid, body):
        response = self.session.put("%s/collections/%s" % (self.BASE_URL, uid), json=body)
        response.raise_for_status()
        return response.json()

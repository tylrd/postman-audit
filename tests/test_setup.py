from postman_audit.postman_client import PostmanClient

import httpretty
import json
import os


def register_collections_meta():
    collections_file = os.path.join(os.path.dirname(__file__), "data/collections.json")
    with open(collections_file) as f:
        collections_data = f.read()
    httpretty.register_uri(httpretty.GET, PostmanClient.BASE_URL + "/collections", body=collections_data, status=201,
                           content_type='application/json')


def register_collections():
    path = os.path.join(os.path.dirname(__file__), "data/collections")
    files = [os.path.join(path, f) for f in os.listdir(path) if os.path.isfile(os.path.join(path, f))]
    for collection_file in files:
        with open(collection_file) as f:
            raw = f.read()
            data = json.loads(raw)
            httpretty.register_uri(httpretty.GET,
                                   PostmanClient.BASE_URL + "/collections/631643-%s" % data['collection']['info']['_postman_id'],
                                   body=raw, status=201, content_type='application/json')

from postman_client import PostmanClient
import logging
import time

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class Auditor:
    def __init__(self, client, delay=5, lazy=False):
        self.client = client  # type: PostmanClient
        self.collections = []
        self.delay = delay
        if not lazy:
            self.initialize()

    def initialize(self):
        data = self.client.get_collections()
        if 'collections' in data:
            for collection in data['collections']:
                if 'name' in collection and 'uid' in collection:
                    logger.info("Retrieving collection: %s", collection['name'])
                    collection_data = self.client.get_collection(collection['uid'])
                    self.collections.append(collection_data)
                    time.sleep(self.delay)
        else:
            logger.info("Json response does not contain array of collections")
        return data

    def audit_headers(self):
        self.__iterate_collections(self.__audit_headers)

    @staticmethod
    def __audit_headers(item):
        if 'request' in item and 'header' in item['request']:
            for header in item['request']['header']:
                logger.info("Header: [ %s: %s ]", header['key'], header['value'])

    def audit_prerequest(self):
        self.__iterate_collections(self.__audit_prerequest)

    @staticmethod
    def __audit_prerequest(item):
        if 'event' in item:
            for event in item['event']:
                if 'listen' in event and event['listen'] == 'prerequest' and 'script' in event:
                    if 'exec' in event['script']:
                        script = event['script']['exec']
                        for line in script:
                            logger.info("%s", line)

    def __iterate_collections(self, method):
        for collection in self.collections:
            items = collection['collection']['item']
            if items:
                self.__process_items(items, method)
            else:
                logger.debug("No item found in collection %s", collection['info']['name'])

    def __process_items(self, items, method):
        for item in items:
            if 'item' in items:
                self.__process_items(items['item'], method)
            else:
                method(item)

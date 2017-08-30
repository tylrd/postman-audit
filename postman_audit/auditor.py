from postman_audit.postman_client import PostmanClient
import logging
import time
import click

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class PostmanAuditor:
    def __init__(self, client=None, delay=5, lazy=False, postman_key=None):
        self.client = client if client else PostmanClient(key=postman_key)
        self.collections = []
        self.delay = delay
        if not lazy:
            self.initialize()

    def initialize(self):
        data = self.client.get_collections()
        if 'collections' in data:
            with click.progressbar(data['collections'], label="Initializing collections...") as collections:
                for collection in collections:
                    if 'name' in collection and 'uid' in collection:
                        logger.debug("Retrieving collection: %s", collection['name'])
                        collection_data = self.client.get_collection(collection['uid'])
                        self.collections.append(collection_data)
                        time.sleep(self.delay)
        else:
            logger.debug("Json response does not contain array of collections")
        return data

    def audit(self, method):
        self.__iterate_collections(method)

    def __iterate_collections(self, method):
        for collection in self.collections:
            items = collection['collection']['item']
            click.echo("Auditing collection %s" % collection['collection']['info']['name'])
            if items:
                self.__process_items(items, method, collection['collection']['info']['name'])
            else:
                logger.debug("No item found in collection %s", collection['collection']['info']['name'])

    def __process_items(self, items, audit_method, collection_name):
        for item in items:
            if 'item' in items:
                self.__process_items(items['item'], audit_method, collection_name)
            else:
                audit_method(item, collection_name)

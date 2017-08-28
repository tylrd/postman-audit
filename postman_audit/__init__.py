import requests

def main():
    """Entry point to the application"""
    r = requests.get('https://api.github.com/events')
    print(r.text)

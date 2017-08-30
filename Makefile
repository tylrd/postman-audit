.PHONY: all clean install analysis test coverage test-all

all: test

install:
	pip install -q -e .[dev]

analysis:
	flake8 postman_audit --max-line-length=150

test: install analysis
	pytest

coverage: clean analysis
	pip install -q -e .[test]
	coverage run -m pytest tests
	coverage report
	coverage html

test-all: install
	tox

clean:
	find . -name '*.pyc' -exec rm -f {} +
	find . -name '*.pyo' -exec rm -f {} +
	find . -name '*~' -exec rm -f {} +


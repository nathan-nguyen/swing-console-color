# Load environment variables from .env file if it exists
-include .env

# Export variables for sub-make calls
export

# Configuration variables (defaults can be overridden in .env or command line)
PLATFORM ?= pc
USERNAME ?= noiprocs
TYPE ?= client
HOSTNAME ?= localhost
PORT ?= 8080

# Maven command
MVN = mvn

.PHONY: help build clean test run run-client run-server compile package

# Default target
help:
	@echo "Available targets:"
	@echo "  make build       - Clean and compile the project"
	@echo "  make compile     - Compile without cleaning"
	@echo "  make test        - Run tests"
	@echo "  make run         - Run with current settings (TYPE=$(TYPE))"
	@echo "  make run-client  - Run as client"
	@echo "  make run-server  - Run as server"
	@echo "  make package     - Package the application as JAR"
	@echo "  make clean       - Clean build artifacts"
	@echo ""
	@echo "Configuration variables:"
	@echo "  PLATFORM=$(PLATFORM) USERNAME=$(USERNAME) TYPE=$(TYPE)"
	@echo "  HOSTNAME=$(HOSTNAME) PORT=$(PORT)"
	@echo ""
	@echo "Example: make run USERNAME=alice HOSTNAME=192.168.1.100 PORT=9090"

build:
	$(MVN) clean compile

compile:
	$(MVN) compile

test:
	$(MVN) test

clean:
	$(MVN) clean

package:
	$(MVN) package

run:
	$(MVN) exec:java -Dexec.mainClass="com.noiprocs.SwingApp" \
		-Dexec.args="$(PLATFORM) $(USERNAME) $(TYPE) $(HOSTNAME) $(PORT)"

run-client:
	$(MAKE) run TYPE=client

run-server:
	$(MAKE) run TYPE=server

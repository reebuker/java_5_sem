SRC := src
BUILD := build
MAIN := Main

SOURCES := $(shell find $(SRC) -name '*.java')

.PHONY: all run clean

all: $(BUILD)/.stamp

$(BUILD)/.stamp: $(SOURCES)
	javac -d $(BUILD) $(SOURCES)
	touch $@

run: all
	java -cp $(BUILD) $(MAIN)

clean:
	rm -rf $(BUILD)

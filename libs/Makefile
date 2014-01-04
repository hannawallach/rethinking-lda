MALLET_DIR = ../MALLET

all: mallet.jar mallet-deps.jar

# note that this won't automatically remake the .jar if things are
# changed within the MALLET source

mallet.jar:
	ant -buildfile $(MALLET_DIR)/build.xml jar; \
	cp $(MALLET_DIR)/dist/mallet.jar $@

mallet-deps.jar:
	cp $(MALLET_DIR)/dist/mallet-deps.jar $@

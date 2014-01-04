BUILD_DIR = build
LIBS_DIR = libs
SRC_DIR = src
DATA_DIR = data
RESULTS_DIR = results

MAX_HEAP = 1500m

JAVA_FLAGS = -server -enableassertions -Xmx$(MAX_HEAP) -XX:MaxPermSize=500m

CP = $(BUILD_DIR):$(LIBS_DIR)/mallet.jar:$(LIBS_DIR)/mallet-deps.jar

# by default simply compile source code

all: $(BUILD_DIR)

.PHONY: $(BUILD_DIR)

# compilation is handled by ant

$(BUILD_DIR): #clean
	ant build

# experiments...

.PRECIOUS: $(DATA_DIR)/%

$(DATA_DIR)/%: $(DATA_DIR)/%.tar.gz
	tar zxvf $< -C $(@D)

$(DATA_DIR)/%.dat: $(DATA_DIR)/%
	java $(JAVA_FLAGS) \
	-classpath $(CP) \
	cc.mallet.classify.tui.Text2Vectors \
	--keep-sequence \
	--output $@ \
	--input $<

$(DATA_DIR)/%_no_stop.dat: $(DATA_DIR)/%
	java $(JAVA_FLAGS) \
	-classpath $(CP) \
	cc.mallet.classify.tui.Text2Vectors \
	--keep-sequence \
	--remove-stopwords \
	--extra-stopwords $(DATA_DIR)/stopword_list.txt \
	--output $@ \
	--input $<

$(DATA_DIR)/nips: $(DATA_DIR)/nips.tar.gz
	tar zxvf $< -C $(@D)

$(DATA_DIR)/nips/orig_data.txt: $(DATA_DIR)/nips

$(DATA_DIR)/nips/data.txt: $(DATA_DIR)/nips/orig_data.txt
	./scripts/process_nips_data.sh $< > $@

$(DATA_DIR)/nips/train.txt: $(DATA_DIR)/nips/data.txt
	head -400 $< > $@

$(DATA_DIR)/nips/test.txt: $(DATA_DIR)/nips/data.txt
	tail -714 $< > $@

$(DATA_DIR)/nips/%_no_stop.dat: $(DATA_DIR)/nips/%.txt
	java $(JAVA_FLAGS) \
	-classpath $(CP) \
	cc.mallet.classify.tui.Csv2Vectors \
	--keep-sequence \
	--remove-stopwords \
	--stoplist-file $(DATA_DIR)/stopwordlist.txt \
	--output $@ \
	--input $<

$(RESULTS_DIR)/hlda/%/T$(T)-S$(S)-SYM$(SYM)-OPT$(OPT)-$(PATH_ASSN)-ID$(ID): $(BUILD_DIR)
	mkdir -p $@; \
	I=`expr $(S) / 10`; \
	java $(JAVA_FLAGS) \
	-classpath $(CP) \
	edu.umass.cs.iesl.wallach.hierarchical.HLDAExpt \
        $(DATA_DIR)/$*.dat \
        $(T) \
	$(PATH_ASSN) \
        $(S) \
        20 \
        $$I \
	$(SYM) \
        $(OPT) \
        $@ \
        > $@/stdout.txt

$(RESULTS_DIR)/hlda/%/T$(T)-S$(S)-SYM$(SYM)-OPT$(OPT)-$(PATH_ASSN)-ID$(ID)/topics.txt:
	NUM=`expr $(T) + 2`; \
	tail -n $${NUM} $(@D)/stdout.txt | head -n $(T) > $@

$(RESULTS_DIR)/hlda/%/T$(T)-S$(S)-SYM$(SYM)-OPT$(OPT)-$(PATH_ASSN)-ID$(ID)/stop.txt: $(RESULTS_DIR)/hlda/%/T$(T)-S$(S)-SYM$(SYM)-OPT$(OPT)-$(PATH_ASSN)-ID$(ID)/topics.txt $(BUILD_DIR)
	java $(JAVA_FLAGS) \
	-classpath $(CP) \
	edu.umass.cs.iesl.wallach.hierarchical.PercentStop \
	$(DATA_DIR)/stopwordlist.txt \
	$(@D)/topics.txt

$(RESULTS_DIR)/lda/%/T$(T)-S$(S)-SYM$(SYM)-OPT$(OPT)-ID$(ID): $(BUILD_DIR)
	mkdir -p $@; \
	I=`expr $(S) / 10`; \
	java $(JAVA_FLAGS) \
	-classpath $(CP) \
	edu.umass.cs.iesl.wallach.hierarchical.LDAExpt \
	$(DATA_DIR)/$*.dat \
	$(T) \
	$(S) \
	20 \
	$$I \
	$(SYM) \
	$(OPT) \
	$@ \
	> $@/stdout.txt

$(RESULTS_DIR)/lda/%/T$(T)-S$(S)-SYM$(SYM)-OPT$(OPT)-ID$(ID)/topics.txt:
	NUM=`expr $(T) + 2`; \
	tail -n $${NUM} $(@D)/stdout.txt | head -n $(T) > $@

$(RESULTS_DIR)/lda/%/T$(T)-S$(S)-SYM$(SYM)-OPT$(OPT)-ID$(ID)/stop.txt: $(RESULTS_DIR)/lda/%/T$(T)-S$(S)-SYM$(SYM)-OPT$(OPT)-ID$(ID)/topics.txt $(BUILD_DIR)
	java $(JAVA_FLAGS) \
	-classpath $(CP) \
	edu.umass.cs.iesl.wallach.hierarchical.PercentStop \
	$(DATA_DIR)/stopwordlist.txt \
	$(@D)/topics.txt

clean:
	ant clean

SRC_DIR    = $(PWD)/src
SRC_FILES	:= $(wildcard $(SRC_DIR)/*.tex)
OUT_DIR    = $(PWD)/dist
OUT_FILES := $(wildcard $(OUT_DIR)/*)
TEX_DIR    = $(PWD)/tex
TOR_DIR    = $(PWD)/torrent
NAME       = dynamo-not-just-for-datastores

default: clean setup compile

clean:
	@rm -rf $(OUT_FILES)

setup:
	@mkdir -p $(OUT_DIR)

env:
	@export TEXINPUTS=$(TEX_DIR):$(HOME)/texmf

compile: slides

run: compile view_slides

abstract: env
	@xelatex -shell-escape -output-directory $(OUT_DIR) $(SRC_DIR)/abstract.tex

slides: env
	@xelatex -shell-escape -output-directory $(OUT_DIR) $(SRC_DIR)/slides.tex

view_abstract:
	@open $(OUT_DIR)/abstract.pdf

view_slides:
	@open $(OUT_DIR)/slides.pdf

torrentize: compile
	@cp $(OUT_DIR)/abstract.pdf $(TOR_DIR)/$(NAME)-abstract.pdf
	@cp $(OUT_DIR)/slides.pdf $(TOR_DIR)/$(NAME)-slides.pdf
	@touch $(TOR_DIR)/README.md

proof:
	echo "Weasel words: "
	$(HOME)/bin/findweaselwords $(SRC_FILES)

	echo ""
	echo "Passive voice: "
	$(HOME)/bin/findpassivevoice $(SRC_FILES)

	echo ""
	echo "Duplicates: "
	$(HOME)/bin/findlexicalillusions $(SRC_FILES)

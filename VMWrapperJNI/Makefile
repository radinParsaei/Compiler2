ifeq ($(OS),Windows_NT)
	OBJEXT:=dll
else
	UNAME := $(shell uname -s)
	ifeq ($(UNAME),Linux)
			OBJEXT:=so
	endif
	ifeq ($(UNAME),Darwin)
			OBJEXT=dylib
	endif
endif

.PHONY: all
all: libVMWrapper.$(OBJEXT)

VMWrapper.h: ../src/main/java/com/example/VMWrapper.java
	javac -h . ../src/main/java/com/example/{VMWrapper.java,SyntaxTree.java,Tool.java,Utils.java,ScopeTool.java,Errors.java,Generator.java}
	$(RM) ../src/main/java/com/example/*.class

VMWrapper.o: VMWrapper.h VMWrapper.cpp
	$(CXX) -c -std=c++14 -fPIC -I../VM/Value -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -I${JAVA_HOME}/include/linux -I${JAVA_HOME}/include/win32 VMWrapper.cpp -o VMWrapper.o

libVMWrapper.$(OBJEXT): VMWrapper.o
	bash ../VM/build_static_call_list.sh "$(BUNDLE_LIBRARIES)" > static_call.cpp
	$(CXX) -std=c++14 -shared -fPIC -I../VM/Value -I../VM ../VM/VM.cpp ../VM/disassembler.cpp static_call.cpp -o libVMWrapper.$(OBJEXT) VMWrapper.o -lc $(LDFLAGS)

.PHONY: clean
clean:
	$(RM) *.h *.o *.{dylib,so,dll}

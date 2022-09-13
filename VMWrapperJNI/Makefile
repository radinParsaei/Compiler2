ifeq ($(OS),Windows_NT)
	OBJECT:=dll
else
	UNAME := $(shell uname -s)
	ifeq ($(UNAME),Linux)
			OBJECT:=so
	endif
	ifeq ($(UNAME),Darwin)
			OBJECT=dylib
	endif
endif

all: libVMWrapper.$(OBJECT) libNativeVM.a
PKGNAME=com_example_

static_call.cpp:
	bash ../VM/build_static_call_list.sh "$(BUNDLE_LIBRARIES)" > static_call.cpp

$(PKGNAME)VMWrapper.h: ../src/main/java/com/example/VMWrapper.java
	javac -h . ../src/main/java/com/example/{VMWrapper.java,SyntaxTree.java,Tool.java,Utils.java,ScopeTool.java,Errors.java,Generator.java}
	$(RM) ../src/main/java/com/example/*.class

VMWrapper.o: $(PKGNAME)VMWrapper.h VMWrapper.cpp
	$(CXX) -O3 -c -std=c++14 -fPIC -I../VM/Value $(CXXFLAGS) -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -I${JAVA_HOME}/include/linux -I${JAVA_HOME}/include/win32 VMWrapper.cpp -o VMWrapper.o

libVMWrapper.$(OBJECT): VMWrapper.o static_call.cpp
	$(CXX) -O3 -std=c++14 -shared -fPIC -I../VM/Value $(CXXFLAGS) -I../VM ../VM/VM.cpp ../VM/disassembler.cpp static_call.cpp -o libVMWrapper.$(OBJECT) VMWrapper.o -lc $(LDFLAGS)

libNativeVM.a: ./VMWrapper.native.o ./static_call.native.o disassembler.native.o VM.native.o
	$(AR) rcs libNativeVM.a $+

./%.native.o: %.cpp
	$(CXX) -std=c++14 -O3 -DUSE_DOUBLE -c $< -I../VM -I../VM/Value -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -I${JAVA_HOME}/include/linux -I${JAVA_HOME}/include/win32 -o $@

%.native.o: ../VM/%.cpp
	$(CXX) -std=c++14 -O3 -DEXCLUDE_MAIN -DUSE_DOUBLE -c $< -I../VM/Value -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -I${JAVA_HOME}/include/linux -I${JAVA_HOME}/include/win32 -o $@

clean:
	$(RM) *.h *.o libNativeVM.a *.{dylib,so,dll}

.PHONY: all clean

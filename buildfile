# Version number for this release
VERSION_NUMBER = "0.1.0"
# Group identifier for your projects
GROUP = "com.jclark.microxml.tree"

repositories.remote << "http://repo1.maven.org/maven2"

my_layout = Layout.new
my_layout[:source, :main, :java] = 'src'
my_layout[:source, :test, :java] = 'test'
my_layout[:source, :test, :resources] = 'test'

desc "MicroXML"
define "microxml", :layout=>my_layout do

  project.version = VERSION_NUMBER
  project.group = GROUP
  manifest["Implementation-Vendor"] = "James Clark"
  test.using :testng
  test.resources.include('*.json')
  compile.with 'com.intellij:annotations:jar:9.0.4'
  test.compile.with 'com.googlecode.json-simple:json-simple:jar:1.1'
  package :jar, :id => 'microxml'
  doc.using(:link => 'http://docs.oracle.com/javase/6/docs/api/')
end


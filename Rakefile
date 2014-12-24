#!/usr/bin/env rake
require 'rake/testtask'
require 'rubygems'
require 'rubygems/package_task'

require File.expand_path('../lib/murmurhash3/version', __FILE__)

Rake::TestTask.new do |i|
  i.libs << 'ext'
  i.options = '-v'
  i.verbose = true
end

spec = Gem::Specification.new do |gem|
  gem.name          = "murmurhash3"
  gem.authors       = ["Sokolov Yura 'funny-falcon'"]
  gem.email         = ["funny.falcon@gmail.com"]
  gem.description   = %q{implementation of murmur3 hashing function}
  gem.summary       = %q{implements mumur3 hashing function}
  gem.homepage      = "https://github.com/funny-falcon/murmurhash3-ruby"
  gem.license       = "MIT"

  files             = FileList['lib/**/*.rb'] + FileList['test/**/*.rb']
  if RUBY_ENGINE == 'jruby'
    gem.files       = files + FileList['ext/**/*.jar']
  else
    gem.extensions  = ["ext/murmurhash3/extconf.rb"]
    gem.files       = files + FileList['ext/**/*.c']
  end
  gem.test_files    = gem.files.grep(%r{^test/})
  gem.require_paths = ["lib", "ext"]
  gem.version       = MurmurHash3::VERSION

  gem.required_ruby_version = ">= 1.9.1"
  gem.platform      = Gem::Platform::RUBY

  gem.add_development_dependency 'rake-compiler', '~> 0.9'
end

Gem::PackageTask.new(spec) do |pkg|
end

if RUBY_PLATFORM =~ /java/
  require 'rake/javaextensiontask'
  Rake::JavaExtensionTask.new("native", spec) do |ext|
    ext.lib_dir = 'ext/murmurhash3'
    ext.ext_dir = 'ext/murmurhash3'
  end
else
  require 'rake/extensiontask'
  Rake::ExtensionTask.new("native", spec) do |ext|
    ext.lib_dir = 'ext/murmurhash3'
    ext.ext_dir = 'ext/murmurhash3'
  end
end

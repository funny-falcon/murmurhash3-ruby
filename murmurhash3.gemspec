# -*- encoding: utf-8 -*-
require File.expand_path('../lib/murmurhash3/version', __FILE__)

Gem::Specification.new do |gem|
  gem.authors       = ["Sokolov Yura 'funny-falcon'"]
  gem.email         = ["funny.falcon@gmail.com"]
  gem.description   = %q{implementation of murmur3 hashing function}
  gem.summary       = %q{implements mumur3 hashing function}
  gem.homepage      = "https://github.com/funny-falcon/murmurhash3"

  gem.files         = Dir['ext/**/*'].grep(/\.(rb|c)$/) +
                      (Dir['lib/**/*'] + Dir['test/**/*']).grep(/\.rb$/)
  gem.test_files    = gem.files.grep(%r{^test/})
  gem.extensions    = ["ext/murmurhash3/extconf.rb"]
  gem.name          = "murmurhash3"
  gem.require_paths = ["lib", "ext"]
  gem.version       = MurmurHash3::VERSION
end

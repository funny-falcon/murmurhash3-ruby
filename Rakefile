#!/usr/bin/env rake
require "bundler/gem_tasks"

require 'rake/testtask'
Rake::TestTask.new do |i|
  i.libs << 'ext'
  i.options = '-v'
  i.verbose = true
end

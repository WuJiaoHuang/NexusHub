local stockKey = ARGV[1]
local orderKey = ARGV[2]
local compensatedKey = ARGV[3]
local userId = ARGV[4]
local ttlSeconds = tonumber(ARGV[5] or '86400')

if redis.call('exists', compensatedKey) == 1 then
    return 1
end

redis.call('incrby', stockKey, 1)
redis.call('srem', orderKey, userId)
redis.call('set', compensatedKey, '1', 'EX', ttlSeconds)
return 0

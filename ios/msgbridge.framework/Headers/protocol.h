#ifndef __PROTOCOL_H__
#define __PROTOCOL_H__

#include <cstdint>

struct MessageHead{
    std::uint32_t size;
    std::uint16_t version;
    std::uint16_t type;
    std::uint16_t from_module;
    std::uint16_t to_module;
};

#endif
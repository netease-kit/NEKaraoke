// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#include "NMCTPCircularBuffer.h"
#include <mach/mach.h>
#include <stdio.h>
#include <stdlib.h>

#define reportNMCResult(result, operation) \
  (_reportNMCResult((result), (operation), strrchr(__FILE__, '/') + 1, __LINE__))
static inline bool _reportNMCResult(kern_return_t result, const char *operation, const char *file,
                                    int line) {
  if (result != ERR_SUCCESS) {
    printf("%s:%d: %s: %s\n", file, line, operation, mach_error_string(result));
    return false;
  }
  return true;
}

bool _NMCTPCircularBufferInit(NMCTPCircularBuffer *buffer, int32_t length, size_t structSize) {
  assert(length > 0);

  if (structSize != sizeof(NMCTPCircularBuffer)) {
    fprintf(stderr, "NMCTPCircularBuffer: Header version mismatch. Check for "
                    "old versions of NMCTPCircularBuffer in your project\n");
    exit(0);
  }

  // Keep trying until we get our buffer, needed to handle race conditions
  int retries = 3;
  while (true) {
    buffer->length = (int32_t)round_page(length);  // We need whole page sizes

    // Temporarily allocate twice the length, so we have the contiguous address
    // space to support a second instance of the buffer directly after
    vm_address_t bufferAddress;
    kern_return_t result = vm_allocate(mach_task_self(), &bufferAddress, buffer->length * 2,
                                       VM_FLAGS_ANYWHERE);  // allocate anywhere it'll fit
    if (result != ERR_SUCCESS) {
      if (retries-- == 0) {
        reportNMCResult(result, "Buffer allocation");
        return false;
      }
      // Try again if we fail
      continue;
    }

    // Now replace the second half of the allocation with a virtual copy of the
    // first half. Deallocate the second half...
    result = vm_deallocate(mach_task_self(), bufferAddress + buffer->length, buffer->length);
    if (result != ERR_SUCCESS) {
      if (retries-- == 0) {
        reportNMCResult(result, "Buffer deallocation");
        return false;
      }
      // If this fails somehow, deallocate the whole region and try again
      vm_deallocate(mach_task_self(), bufferAddress, buffer->length);
      continue;
    }

    // Re-map the buffer to the address space immediately after the buffer
    vm_address_t virtualAddress = bufferAddress + buffer->length;
    vm_prot_t cur_prot, max_prot;
    result = vm_remap(mach_task_self(),
                      &virtualAddress,   // mirror target
                      buffer->length,    // size of mirror
                      0,                 // auto alignment
                      0,                 // force remapping to virtualAddress
                      mach_task_self(),  // same task
                      bufferAddress,     // mirror source
                      0,                 // MAP READ-WRITE, NOT COPY
                      &cur_prot,         // unused protection struct
                      &max_prot,         // unused protection struct
                      VM_INHERIT_DEFAULT);
    if (result != ERR_SUCCESS) {
      if (retries-- == 0) {
        reportNMCResult(result, "Remap buffer memory");
        return false;
      }
      // If this remap failed, we hit a race condition, so deallocate and try
      // again
      vm_deallocate(mach_task_self(), bufferAddress, buffer->length);
      continue;
    }

    if (virtualAddress != bufferAddress + buffer->length) {
      // If the memory is not contiguous, clean up both allocated buffers and
      // try again
      if (retries-- == 0) {
        printf("Couldn't map buffer memory to end of buffer\n");
        return false;
      }

      vm_deallocate(mach_task_self(), virtualAddress, buffer->length);
      vm_deallocate(mach_task_self(), bufferAddress, buffer->length);
      continue;
    }

    buffer->buffer = (void *)bufferAddress;
    buffer->fillCount = 0;
    buffer->head = buffer->tail = 0;
    buffer->atomic = true;

    return true;
  }
  return false;
}

void NMCTPCircularBufferCleanup(NMCTPCircularBuffer *buffer) {
  vm_deallocate(mach_task_self(), (vm_address_t)buffer->buffer, buffer->length * 2);
  memset(buffer, 0, sizeof(NMCTPCircularBuffer));
}

void NMCTPCircularBufferClear(NMCTPCircularBuffer *buffer) {
  int32_t fillCount;
  if (NMCTPCircularBufferTail(buffer, &fillCount)) {
    NMCTPCircularBufferConsume(buffer, fillCount);
  }
}

void NMCTPCircularBufferSetAtomic(NMCTPCircularBuffer *buffer, bool atomic) {
  buffer->atomic = atomic;
}

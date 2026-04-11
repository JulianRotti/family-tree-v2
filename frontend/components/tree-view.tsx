"use client"

import { motion, AnimatePresence } from "framer-motion"
import { useState, useMemo } from "react"
import { familyMembers, getMemberById, type FamilyMember } from "@/lib/mock-data"
import { cn } from "@/lib/utils"
import { User, Heart, Search, ChevronDown, Users, Layers, Sparkles } from "lucide-react"
import Image from "next/image"
import { GenderIndicator } from "@/components/gender-indicator"

interface TreeNodeProps {
  member: FamilyMember
  isHead: boolean
  onClick?: () => void
}

function TreeNode({ member, isHead, onClick }: TreeNodeProps) {
  const birthYear = new Date(member.birthDate).getFullYear()
  const deathYear = member.deathDate ? new Date(member.deathDate).getFullYear() : null
  const isAlive = !member.deathDate

  return (
    <motion.div
      whileHover={{ scale: 1.02 }}
      whileTap={{ scale: 0.98 }}
      onClick={onClick}
      className={cn(
        "flex flex-col items-center p-4 rounded-xl border transition-all duration-200 min-w-[140px]",
        isHead
          ? "bg-terracotta/20 border-terracotta shadow-lg"
          : "bg-card border-border hover:border-dusty-rose hover:shadow-md cursor-default"
      )}
    >
      <div
        className={cn(
          "w-14 h-14 rounded-full flex items-center justify-center mb-2 transition-colors overflow-hidden",
          isHead ? "ring-2 ring-terracotta" : "",
          !member.imageUrl && (isHead ? "bg-terracotta text-parchment" : "bg-secondary text-muted-foreground")
        )}
      >
        {member.imageUrl ? (
          <Image
            src={member.imageUrl}
            alt={`${member.firstName} ${member.lastName}`}
            width={56}
            height={56}
            className="w-full h-full object-cover"
          />
        ) : (
          <User className="w-6 h-6" />
        )}
      </div>
      <span className="font-serif text-sm font-medium text-foreground text-center flex items-center gap-1">
        {member.firstName}
        <GenderIndicator gender={member.gender} />
      </span>
      <span className="font-serif text-xs text-muted-foreground">
        {member.lastName}
      </span>
      <span className={cn(
        "text-xs mt-1",
        isAlive ? "text-sage" : "text-muted-foreground"
      )}>
        {birthYear}{deathYear ? ` – ${deathYear}` : ' – present'}
      </span>
    </motion.div>
  )
}

function HeadSelector({ 
  selectedId, 
  onSelect 
}: { 
  selectedId: string
  onSelect: (id: string) => void 
}) {
  const [isOpen, setIsOpen] = useState(false)
  const [search, setSearch] = useState('')
  
  const selectedMember = getMemberById(selectedId)
  
  const filteredMembers = useMemo(() => {
    if (!search.trim()) return familyMembers
    const query = search.toLowerCase()
    return familyMembers.filter(m => 
      m.firstName.toLowerCase().includes(query) ||
      m.lastName.toLowerCase().includes(query)
    )
  }, [search])

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-3 px-4 py-2.5 bg-card border border-border rounded-xl hover:border-dusty-rose transition-colors min-w-[240px]"
      >
        {selectedMember?.imageUrl ? (
          <Image
            src={selectedMember.imageUrl}
            alt={selectedMember.firstName}
            width={32}
            height={32}
            className="w-8 h-8 rounded-full object-cover"
          />
        ) : (
          <div className="w-8 h-8 rounded-full bg-secondary flex items-center justify-center">
            <User className="w-4 h-4 text-muted-foreground" />
          </div>
        )}
        <div className="flex-1 text-left">
          <p className="text-sm font-medium text-foreground">
            {selectedMember ? `${selectedMember.firstName} ${selectedMember.lastName}` : 'Select head'}
          </p>
          <p className="text-xs text-muted-foreground">Head of tree</p>
        </div>
        <ChevronDown className={cn(
          "w-4 h-4 text-muted-foreground transition-transform",
          isOpen && "rotate-180"
        )} />
      </button>
      
      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, y: -8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            className="absolute top-full left-0 right-0 mt-2 bg-card border border-border rounded-xl shadow-lg overflow-hidden z-50"
          >
            <div className="p-2 border-b border-border">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <input
                  type="text"
                  placeholder="Search members..."
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  className="w-full pl-9 pr-3 py-2 bg-secondary rounded-lg text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-1 focus:ring-terracotta"
                  autoFocus
                />
              </div>
            </div>
            <div className="max-h-64 overflow-y-auto">
              {filteredMembers.map(member => (
                <button
                  key={member.id}
                  onClick={() => {
                    onSelect(member.id)
                    setIsOpen(false)
                    setSearch('')
                  }}
                  className={cn(
                    "w-full flex items-center gap-3 px-3 py-2.5 hover:bg-secondary transition-colors",
                    member.id === selectedId && "bg-terracotta/10"
                  )}
                >
                  {member.imageUrl ? (
                    <Image
                      src={member.imageUrl}
                      alt={member.firstName}
                      width={28}
                      height={28}
                      className="w-7 h-7 rounded-full object-cover"
                    />
                  ) : (
                    <div className="w-7 h-7 rounded-full bg-muted flex items-center justify-center">
                      <User className="w-3.5 h-3.5 text-muted-foreground" />
                    </div>
                  )}
                  <div className="flex-1 text-left">
                    <p className="text-sm text-foreground flex items-center gap-1">
                      {member.firstName} {member.lastName}
                      <GenderIndicator gender={member.gender} />
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {new Date(member.birthDate).getFullYear()}
                      {member.deathDate ? ` – ${new Date(member.deathDate).getFullYear()}` : ''}
                    </p>
                  </div>
                  {member.id === selectedId && (
                    <span className="text-xs text-terracotta font-medium">Selected</span>
                  )}
                </button>
              ))}
              {filteredMembers.length === 0 && (
                <p className="px-4 py-6 text-sm text-muted-foreground text-center">
                  No members found
                </p>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
      
      {/* Backdrop */}
      {isOpen && (
        <div 
          className="fixed inset-0 z-40" 
          onClick={() => {
            setIsOpen(false)
            setSearch('')
          }} 
        />
      )}
    </div>
  )
}

export function TreeView() {
  const [headId, setHeadId] = useState<string>('1')

  const headMember = getMemberById(headId)

  // Calculate stats
  const stats = useMemo(() => {
    const livingCount = familyMembers.filter(m => !m.deathDate).length
    const earliestYear = Math.min(...familyMembers.map(m => new Date(m.birthDate).getFullYear()))
    
    // Calculate generations (simplified)
    const generations = 5 // This would be calculated properly with full tree traversal
    
    return { livingCount, generations, earliestYear, totalCount: familyMembers.length }
  }, [])

  // Get parents
  const parents = headMember?.parentIds?.map(id => getMemberById(id)).filter(Boolean) as FamilyMember[] || []

  // Get spouse(s)
  const spouses = headMember?.spouseIds?.map(id => getMemberById(id)).filter(Boolean) as FamilyMember[] || []

  // Get children
  const children = headMember?.childIds?.map(id => getMemberById(id)).filter(Boolean) as FamilyMember[] || []

  // Get siblings (same parents)
  const siblings = familyMembers.filter(m =>
    m.id !== headId &&
    m.parentIds?.some(pid => headMember?.parentIds?.includes(pid))
  )

  return (
    <div className="h-full flex flex-col">
      {/* Header with Stats */}
      <div className="px-6 py-5 border-b border-border bg-card/50 backdrop-blur-sm">
        <div className="flex flex-col lg:flex-row lg:items-center gap-4">
          {/* Head Selector */}
          <div className="flex-shrink-0">
            <p className="text-xs text-muted-foreground uppercase tracking-wide mb-2">Center on</p>
            <HeadSelector selectedId={headId} onSelect={setHeadId} />
          </div>
          
          {/* Stats */}
          <div className="flex-1 flex items-center gap-6 lg:justify-end">
            <div className="flex items-center gap-2">
              <div className="w-9 h-9 rounded-lg bg-sage/10 flex items-center justify-center">
                <Layers className="w-4 h-4 text-sage" />
              </div>
              <div>
                <p className="text-lg font-serif font-semibold text-foreground">{stats.generations}</p>
                <p className="text-xs text-muted-foreground">Generations</p>
              </div>
            </div>
            
            <div className="flex items-center gap-2">
              <div className="w-9 h-9 rounded-lg bg-terracotta/10 flex items-center justify-center">
                <Users className="w-4 h-4 text-terracotta" />
              </div>
              <div>
                <p className="text-lg font-serif font-semibold text-foreground">{stats.totalCount}</p>
                <p className="text-xs text-muted-foreground">Members</p>
              </div>
            </div>
            
            <div className="flex items-center gap-2">
              <div className="w-9 h-9 rounded-lg bg-dusty-rose/10 flex items-center justify-center">
                <Sparkles className="w-4 h-4 text-dusty-rose" />
              </div>
              <div>
                <p className="text-lg font-serif font-semibold text-foreground">{stats.livingCount}</p>
                <p className="text-xs text-muted-foreground">Living</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Tree Canvas */}
      <div className="flex-1 overflow-auto p-8">
        <div className="flex flex-col items-center gap-6 min-w-max">
          {/* Parents Row */}
          {parents.length > 0 && (
            <motion.div
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              className="flex flex-col items-center gap-2"
            >
              <p className="text-xs text-muted-foreground uppercase tracking-wide">Parents</p>
              <div className="flex items-center gap-4">
                {parents.map((parent, idx) => (
                  <div key={parent.id} className="flex items-center">
                    <TreeNode
                      member={parent}
                      isHead={false}
                      onClick={() => setHeadId(parent.id)}
                    />
                    {idx === 0 && parents.length > 1 && (
                      <div className="mx-2 text-dusty-rose">
                        <Heart className="w-4 h-4" />
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </motion.div>
          )}

          {/* Connection to selected */}
          {parents.length > 0 && (
            <div className="h-8 w-px bg-border" style={{ backgroundImage: 'linear-gradient(to bottom, var(--color-border) 50%, transparent 50%)', backgroundSize: '1px 8px' }} />
          )}

          {/* Selected + Spouse Row */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="flex items-center gap-4"
          >
            {headMember && (
              <>
                <TreeNode
                  member={headMember}
                  isHead={true}
                />
                {spouses.map((spouse) => (
                  <div key={spouse.id} className="flex items-center">
                    <div className="mx-2 text-dusty-rose">
                      <Heart className="w-4 h-4" />
                    </div>
                    <TreeNode
                      member={spouse}
                      isHead={false}
                      onClick={() => setHeadId(spouse.id)}
                    />
                  </div>
                ))}
              </>
            )}
          </motion.div>

          {/* Siblings Row */}
          {siblings.length > 0 && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.1 }}
              className="mt-4"
            >
              <p className="text-xs text-muted-foreground uppercase tracking-wide mb-3 text-center">Siblings</p>
              <div className="flex items-center gap-3 flex-wrap justify-center">
                {siblings.map((sibling) => (
                  <TreeNode
                    key={sibling.id}
                    member={sibling}
                    isHead={false}
                    onClick={() => setHeadId(sibling.id)}
                  />
                ))}
              </div>
            </motion.div>
          )}

          {/* Connection to children */}
          {children.length > 0 && (
            <div className="h-8 w-px bg-border" style={{ backgroundImage: 'linear-gradient(to bottom, var(--color-border) 50%, transparent 50%)', backgroundSize: '1px 8px' }} />
          )}

          {/* Children Row */}
          {children.length > 0 && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.15 }}
              className="flex flex-col items-center gap-3"
            >
              <p className="text-xs text-muted-foreground uppercase tracking-wide">Children</p>
              <div className="flex items-center gap-4 flex-wrap justify-center">
                {children.map((child) => (
                  <TreeNode
                    key={child.id}
                    member={child}
                    isHead={false}
                    onClick={() => setHeadId(child.id)}
                  />
                ))}
              </div>
            </motion.div>
          )}
        </div>
      </div>
    </div>
  )
}
